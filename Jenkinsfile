pipeline {
    agent { label 'project06-agent' }

    environment {
        APP_IMAGE = 'localhost:5000/project06-app'
        KIND_NODE = 'project06-control-plane'
        K8S_NAMESPACE = 'sample-app'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    triggers {
        pollSCM('H/2 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                sh '''
                    JAVA_HOME=/opt/java8 \
                    PATH=/opt/java8/bin:/usr/share/maven/bin:$PATH \
                    mvn clean verify
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('project06-sonarqube') {
                    sh '''
                        JAVA_HOME=/opt/java/openjdk \
                        PATH=/opt/java/openjdk/bin:$PATH \
                        mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                          -Dsonar.token=$SONAR_AUTH_TOKEN \
                          -Dsonar.projectKey=project06-local \
                          -Dsonar.projectName="Project 06 Local" \
                          -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                          -Dsonar.coverage.exclusions=**/DemoWorkshopApplication.java
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build and Push Image') {
            steps {
                sh '''
                    docker build \
                      -t $APP_IMAGE:$BUILD_NUMBER \
                      -t $APP_IMAGE:latest .
                    docker push $APP_IMAGE:$BUILD_NUMBER
                    docker push $APP_IMAGE:latest
                '''
            }
        }

        stage('Deploy to kind') {
            steps {
                sh '''
                    docker save $APP_IMAGE:latest | \
                      docker exec -i $KIND_NODE ctr --namespace=k8s.io images import -

                    docker exec -i $KIND_NODE kubectl apply -f - <<EOF
apiVersion: v1
kind: Namespace
metadata:
  name: $K8S_NAMESPACE
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sample-app
  namespace: $K8S_NAMESPACE
spec:
  replicas: 1
  selector:
    matchLabels:
      app: sample-app
  template:
    metadata:
      labels:
        app: sample-app
    spec:
      containers:
        - name: sample-app
          image: $APP_IMAGE:latest
          imagePullPolicy: Never
          ports:
            - name: http
              containerPort: 8000
---
apiVersion: v1
kind: Service
metadata:
  name: sample-app-service
  namespace: $K8S_NAMESPACE
spec:
  type: ClusterIP
  selector:
    app: sample-app
  ports:
    - name: http
      port: 8000
      targetPort: 8000
EOF

                    docker exec $KIND_NODE kubectl rollout restart \
                      deployment/sample-app -n $K8S_NAMESPACE
                    docker exec $KIND_NODE kubectl rollout status \
                      deployment/sample-app -n $K8S_NAMESPACE --timeout=180s
                '''
            }
        }

        stage('Verify Deployment') {
            steps {
                sh '''
                    docker exec $KIND_NODE kubectl get pods,service \
                      -n $K8S_NAMESPACE
                '''
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
        }
        success {
            echo 'CI/CD pipeline completed successfully.'
        }
        failure {
            echo 'Pipeline failed. Review the failed stage and its console output.'
        }
    }
}
