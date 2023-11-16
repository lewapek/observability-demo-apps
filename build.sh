export VERSION=$(head -1 version)

if [ "$1" = "build" ]; then
  echo "Building version ${VERSION}"
  docker build --build-arg "APP_VERSION=${VERSION}" -t lewap/workshop-products:"${VERSION}" .
fi

if [ "$1" = "push" ]; then
  echo "Pushing version ${VERSION}"
  docker push lewap/workshop-products:"${VERSION}"
fi

if [ "$1" = "run" ]; then
  docker run --rm lewap/workshop-products:"${VERSION}"
fi
