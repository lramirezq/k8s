# k8s
Scripting Kubernetes

# GetSecrets 

Es una clase que permite conectar a AWS Secrets Manager y obtener los datos asociados a un secreto en formato JSON

ejemplo:

{
   "user":"lramirez",
   "password":"myPassword",
   "endpoint":"test.lramirez.cl"
}

# TestSQS

Es una clase que permite assumir un AssumeRoleWithWebIdentity para poder utilizar el rol asignado a un contenedor mediante systemAccount en K8s, despues de hacer la conexión en el metodo hay un proceso para testear el cual agrega un mensaje a una cola SQS.
