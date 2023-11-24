VERSION=$(head -1 version)
all="product order view"

if [ ${#} -ne 2 ]; then
  echo "2 arguments expected: <command> <module>|\"all\""
  exit 1
fi

if [ ${2} = "all" ]; then
  modules=${all}
else
  modules=${2}
fi

for m in ${modules}; do
  name=workshop-${m}
  echo "--- Module: ${m}. Name: ${name}."

  if [ "$1" = "build" ]; then
    echo "Building version ${VERSION}"
    docker build --build-arg "APP_VERSION=${VERSION}" --build-arg "MODULE=${m}" -t lewap/${name}:"${VERSION}" .
  fi

  if [ "$1" = "push" ]; then
    echo "Pushing version ${VERSION}"
    docker push lewap/${name}:"${VERSION}"
  fi
done

echo "Done"
