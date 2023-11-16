if [ $# -ne 1 ]; then
  echo "One argument required"
  exit 1
fi

container_name=local-postgres
user=dontletexpireuser
pass=default
db_name=dontletexpiredb

case $1 in
  "r" | "restart" )
    echo "Restarting postgres"
    docker rm -f ${container_name} 2>/dev/null && \
    docker run --name ${container_name} -d --restart unless-stopped --memory "1.5g" --cpus "1.0" -p 5432:5432 \
     -e POSTGRES_USER=${user} \
     -e POSTGRES_PASSWORD=${pass} \
     -e POSTGRES_DB=${db_name} \
     postgres:14-alpine \
     postgres -c log_statement=all
    ;;
  "e" | "exec" )
    echo "Executing into postgres"
    docker exec -it ${container_name} psql -U ${user} -d ${db_name}
    ;;
  "logs" )
    echo "Showing logs"
    docker logs ${container_name} -f
    ;;
  * )
    echo "Incorrect argument"
    exit 2
    ;;
esac
