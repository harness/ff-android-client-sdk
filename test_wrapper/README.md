# Test wrapper module

## Configuration

Make sure that the configuration file is defined (`wrapper.json`):

```json
{
  "selfTest": true,
  "port": 4000,
  "apiKey": "YOUR_API_KEY"
}
```

## Running test wrapper inside a container

Or, to run test wrapper inside the Docker container execute the following sample command:

```
docker build --build-arg PORT=4000 --build-arg SELF_TEST=false --build-arg \ 
    API_KEY=~YOUR_API_KEY --build-arg WRAPPERS_BRANCH=main -t <image_tag> . && \ 
    docker run -p 0.0.0.0:4000:4000 --name android_test_wrapper <image_tag> 
```

Where the following arguments must be provided:

- `PORT` represents the port that will be used
- `API_KEY` represents your FF API KEY.

Docker image will be created and container started.

## Using test wrapper

Test wrapper will be listening for the API calls on provided port. The following CURL commands 
illustrate the use:

- Ping:

```
curl -X GET -H "content-type: application/json" http://localhost:4000/api/1.0/ping
```

- Feature flag check:

```
curl -X POST -d '{"flag_kind": "boolean", "flag_key": "flag1", "target": {"target_identifier": "test", "target_name": "test"}}' \
 -H "content-type: application/json" http://localhost:4000/api/1.0/check_flag
```
