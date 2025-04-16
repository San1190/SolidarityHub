# Antes de esto ejecutar docker compose up -d

docker exec -it solidarityhub_mysql-dev mysql -u root -proot -D solidarityhub_db

docker exec -it solidarityhub_mysql mysql -u root -proot -D solidarityhub_db

# Despues ejecutar docker compose down (cuando se quiera eliminar el contenedor)