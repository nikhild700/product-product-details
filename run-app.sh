clear
echo "Starting $1 server..."
mvn exec:java -Dexec.mainClass=com.example.server.$1
