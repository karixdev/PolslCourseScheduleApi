docker login || { echo "Could not login into dockerhub"; exit 1; }

service=$1
version=$2
user=$3

src="$service:$version"
tag="$user/polsl-course-schedule-api-$service:$version"

docker tag "$src" "$tag" || { echo "Could not create tag for $src"; exit 1; }
docker push "$tag" || { echo "Could not publish image $tag"; exit 1; }