if [ $# -lt 1 ]; then
  echo "At least 1 argument required"
  exit 1
fi

container_name=workshop-postgres
user=workshop
pass=workshop

case $1 in
  "r" | "restart" )
    echo "Restarting postgres"
    docker rm -f ${container_name} 2>/dev/null && \
    docker run --name ${container_name} -d --restart unless-stopped --memory "1.5g" --cpus "1.0" -p 5432:5432 \
     -e POSTGRES_USER=${user} \
     -e POSTGRES_PASSWORD=${pass} \
     -v $(pwd)/init-pg:/docker-entrypoint-initdb.d \
     postgres:14-alpine \
     postgres -c log_statement=all
    ;;
  "e" | "exec" )
    echo "Executing into postgres"
    if [ $# -ne 2 ]; then
      echo "2 arguments required"
      exit 2
    fi
    docker exec -it ${container_name} psql -U ${user} -d $2
    ;;
  "logs" )
    echo "Showing logs"
    docker logs ${container_name} -f
    ;;
  * )
    echo "Incorrect argument"
    exit 3
    ;;
esac
