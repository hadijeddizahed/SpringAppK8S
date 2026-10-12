# Platform Engineer Roadmap: Spring Boot, Kubernetes, Helm & AWS

This project is a hands-on, step-by-step roadmap designed for transitioning from a **Senior Java Developer** with AWS certification into a **Platform Engineer**.

---

## 📖 Core Concepts Explained (Quick Reference)

- **Helm Chart:** A package manager for Kubernetes. Instead of writing separate, hardcoded YAML files for each environment, Helm lets you define reusable templates and pass configuration values dynamically via `values.yaml`.
- **Deployment:** A Kubernetes controller that declares how many copies (replicas) of your application should run, how rolling updates are performed, and automatically replaces failed pods.
- **Service (NodePort / ClusterIP):** An internal load balancer and stable network endpoint in front of your pods, ensuring traffic still reaches the app even when pod IP addresses change.
- **ConfigMap:** A Kubernetes object used to store non-sensitive configuration outside the container image, following 12-factor principles.
- **Liveness Probe:** A periodic check asking: *"Is the container still healthy?"* If the application deadlocks or stops responding, Kubernetes terminates and restarts the container.
- **Readiness Probe:** A periodic check asking: *"Is the application ready to serve traffic?"* During boot-up or dependency outage, Kubernetes halts routing traffic to that pod without restarting it.
- **Resource Requests & Limits:** Boundaries defining minimum reserved resources (*Requests*) and maximum ceiling (*Limits*) to prevent node starvation.
- **Graceful Shutdown:** Allowing in-flight HTTP requests and resources to finish cleanly when receiving a `SIGTERM` signal before stopping.
- **GitOps & ArgoCD:** A declarative continuous delivery model where **Git is the single source of truth**. ArgoCD continuously reconciles the live Kubernetes cluster state with the desired state declared in Git, automatically detecting and healing configuration drift.

---

## 🗺️ Overall Roadmap

```text
[Phase 1: Production-Ready Application in K8s (Completed)]
   ├── Spring Boot Actuator (Liveness & Readiness Probes)
   ├── ConfigMap & Environment Variables (12-Factor App)
   ├── Resource Requests & Limits (Prevent OOMKilled)
   └── Graceful Shutdown (Zero-downtime Rollout)

[Phase 2: GitOps with ArgoCD (Installed & Configured)]
   ├── ArgoCD Controller & Server deployment
   ├── Declarative Application CRD (argocd-application.yaml)
   └── Automated Sync & Self-Healing

[Phase 3: Observability & Metrics (Next Step)]
   ├── Prometheus & Metrics (JVM metrics via Micrometer)
   └── Grafana Dashboards

[Phase 4: Cloud Infrastructure on AWS]
   ├── Terraform (Provisioning VPC & EKS Cluster)
   ├── AWS IAM Roles for Service Accounts (IRSA)
   └── AWS Load Balancer Controller (Ingress instead of NodePort)
```

---

## 🚀 Phase 2: GitOps with ArgoCD (Completed)

ArgoCD is now running inside the Minikube cluster under the `argocd` namespace.

### How to Access the ArgoCD Web UI

1. Start port-forwarding the ArgoCD server:
   ```bash
   kubectl port-forward svc/argocd-server -n argocd 8085:443
   ```
2. Open your browser at:  
   **[https://localhost:8085](https://localhost:8085)** *(Accept the self-signed TLS warning)*
3. Login credentials:
   - **Username:** `admin`
   - **Password:** Retrieve dynamically with:
     ```bash
     kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d && echo ""
     ```

### Managing the App via GitOps (`argocd-application.yaml`)

An ArgoCD custom resource manifest [`argocd-application.yaml`](file:///Users/hadi/samples/SpringAppK8S/argocd-application.yaml) has been added to the project:
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: spring-boot-app
  namespace: argocd
spec:
  project: default
  source:
    repoURL: 'https://github.com/YOUR_USERNAME/SpringAppK8S.git'
    targetRevision: HEAD
    path: helm/spring-app
    helm:
      valueFiles:
        - values.yaml
  destination:
    server: 'https://kubernetes.default.svc'
    namespace: default
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
```

When you push this repository to Git and apply this file (`kubectl apply -f argocd-application.yaml`):
- ArgoCD takes over lifecycle management of the Helm chart.
- Any manual change in the cluster that doesn't match Git is automatically corrected (**Self-Healing**).
- Any commit merged to your branch is automatically rolled out to the cluster without developer intervention.

---

## 📁 Project Structure

```text
SpringAppK8S/
├── pom.xml                                  # Spring Boot dependencies + Actuator
├── Dockerfile                               # Temurin Alpine JRE 21 image
├── argocd-application.yaml                  # Declarative ArgoCD GitOps Application manifest
├── src/main/
│   ├── java/com/example/demo/
│   │   └── Application.java                 # Spring Boot app with REST endpoint & @Value injection
│   └── resources/
│       └── application.properties           # Probes, shutdown timeout & env variable fallback
└── helm/
    └── spring-app/                          # Helm Chart
        ├── Chart.yaml                       # Chart metadata
        ├── values.yaml                      # Configurable values (replicas, image, resources, probes)
        └── templates/
            ├── deployment.yaml              # Deployment manifest with probes, limits, and env injection
            ├── configmap.yaml               # ConfigMap template for environment variables
            └── service.yaml                 # NodePort Service template
```

---

## 🛠️ Step-by-Step CLI Execution Guide

### 1. Build and Run App Locally in Minikube
```bash
mvn clean package -DskipTests
minikube image build -t spring-app-k8s:1.1.0 .
helm upgrade --install my-spring-app ./helm/spring-app
```

### 2. Verify Status & Health Probes
```bash
kubectl get pods,svc,configmap -l app=spring-app
kubectl exec deploy/my-spring-app-deployment -- wget -qO- http://localhost:8080/actuator/health
```

### 3. Check ArgoCD Status
```bash
kubectl get pods -n argocd
```

---

## 💡 Key Helm & ArgoCD Commands

| Command | Purpose |
| :--- | :--- |
| `helm list` | List installed Helm releases |
| `helm upgrade my-spring-app ./helm/spring-app` | Upgrade release locally |
| `kubectl port-forward svc/argocd-server -n argocd 8085:443` | Access ArgoCD UI locally |
| `kubectl apply -f argocd-application.yaml` | Register GitOps app in ArgoCD |
