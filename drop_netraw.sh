#script para quitar capability NET_RAW en todos los deployment del Cluster EKS

namespaces=$(kubectl get namespaces | cut -d " " -f1)
rm -rf *.json
for n in $namespaces 
do
  if [ $n != "NAME" ]; then
    echo "BUSCANDO DEPLOYMENT PARA EL NAMESPACE [$n]"
    deployments=$(kubectl get deployment -n $n | cut -d " " -f1)
   
    
    for d in $deployments
    do
      if [ $d != "NAME" ]; then
        echo "Deployment [$d]"
        
        #aqui editar el deployment
        kubectl get deployment $d -n $n -o json > $n-$d.json
        
        cat $n-$d.json| jq -r .spec.template.spec.containers[].securityContext='{ "capabilities": { "drop": [ "NET_RAW" ] }}' > $n-netraw-$d.json
        
        kubectl apply -f $n-netraw-$d.json -n $n
      fi
    done 
    
  fi 
  echo "==============================================================="
done
rm -rf *.json
