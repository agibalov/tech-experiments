rm -rf ./angular
docker run \
  --rm \
  --user $(id -u):$(id -g) \
  --volume ${PWD}:/local \
  openapitools/openapi-generator-cli:v3.3.4 generate \
  --input-spec /local/api.yaml \
  --generator-name typescript-angular \
  --template-dir /local/templates \
  --config /local/config.json \
  --output /local/angular
