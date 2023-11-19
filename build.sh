export VERSION=$(head -1 version)

name=workshop-products

if [ "$1" = "build" ]; then
  echo "Building version ${VERSION}"
  docker build --build-arg "APP_VERSION=${VERSION}" -t lewap/${name}:"${VERSION}" .
fi

if [ "$1" = "push" ]; then
  echo "Pushing version ${VERSION}"
  docker push lewap/${name}:"${VERSION}"
fi

if [ "$1" = "run" ]; then
  docker run --rm lewap/$name}:"${VERSION}"
fi
