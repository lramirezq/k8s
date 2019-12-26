#!/bin/bash
#nombreconfigmap
configmap_name=${p:configmap}
namespace=${p:namespace}

if [ $configmap_name != "" ]; then


export TOKEN=`aws ecr get-authorization-token --region $AWS_DEFAULT_REGION --output text --query 'authorizationData[].authorizationToken' |base64 -d|cut -d: -f2`
aws eks --region us-east-1 update-kubeconfig --name ${p:cluster_name}
  if [ $? -ne 0 ]
    then
        echo "Error al Obtener configuracion de Cluster".
        exit 1
  fi
docker login -u AWS -p $TOKEN $AWS_REPO



#borrar archivo temporal 
if [ -f lrq_archivos.txt" ]; then
  rm -f lrq_archivos.txt
fi

if [ -f "lrq_configmap.yml" ]; then
  rm -f lrq_configmap.yml
fi


#buscar archivos a agregar al configmap

echo "${p:system/filesConfigMaps}" > filesConfigMaps.txt
input="filesConfigMaps.txt"
command_find="find . -name ll.txt"
while IFS= read -r line
do
  command_find="$command_find -o -name"
  command_find="$command_find $line"
done < "$input"
echo $command_find 
$command_find >  lrq_archivos.txt
  
#  cat app_files_config.txt >> lrq_archivos.txt


[ ! -d "tmp" ] && mkdir tmp && touch ./tmp/config-map.json


#traer archivos existentes a tmp
CM_FILE=./tmp/config-map.json
kubectl -n $namespace get configmap  $configmap_name -o json > $CM_FILE
if [ $? -ne 0 ]
    then
        echo "Error al Obtener Informacion de ConfigMap $configmap_name".
        exit 1
fi



DATA_FILES_DIR=./tmp
#archivos existentes en configmap
echo "Get ConfigMapOriginal"
files=$(cat $CM_FILE | jq '.data' | jq -r 'keys[]')
for k in $files; do
    name=".data[\"$k\"]"
    cat $CM_FILE | jq -r $name > $DATA_FILES_DIR/$k;
done

echo "Buscando Archivos a Reemplazar en ConfigMap "
for k in $files; do
  #echo $k
  
  input="lrq_archivos.txt"
  while IFS= read -r line
  do
    if [ $k == $(basename $line) ]
    then
      echo "Se reemplazara [$k] -  [$line]"
      rm -rf ./tmp/$k
    fi 
  done < "$input"

done

#volvemos a hacer la busqueda
echo "${p:system/filesConfigMaps}" > filesConfigMaps.txt
input="filesConfigMaps.txt"
command_find="find . -name ll.txt"
while IFS= read -r line
do
  command_find="$command_find -o -name"
  command_find="$command_find $line"
done < "$input"
echo $command_find 
$command_find >  lrq_archivos.txt

# cat app_files_config.txt >> lrq_archivos.txt 
 
input="lrq_archivos.txt"
comando=""
while IFS= read -r line
do
  echo "Agregando... [$line] a ConfigMap [$configmap_name] "
  
  comando="$comando --from-file $line"
done < "$input"

if [ "$comando" != "" ]
then
  #creando file en runtime
  kubectl create -n $namespace configmap $configmap_name $comando -o yaml --dry-run > lrq_configmap.yml
  if [ $? -ne 0 ]
      then
          echo "Error al Crear ConfigMap Temporal".
          exit 1
  fi
  #actualizando el contenido cambios
  kubectl apply -n $namespace  -f lrq_configmap.yml
  if [ $? -ne 0 ]
      then
          echo "Error al Aplicar Cambios al ConfigMap".
          exit 1
  fi
else
  echo "NO se encuentran archivos de configuracion"
fi

else
  echo "No existe ConfigMap para aplicar"
fi
