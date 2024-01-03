service=$1
version=$2

cd "$service" || { echo "Could not cd into $service"; exit 1; }

src="$service:$version"

docker build -t "$src" . || { echo "Could not create image $src"; exit 1; }

echo "Created image: $service:$version"