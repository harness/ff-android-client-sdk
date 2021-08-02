# Test wrapper module

## Configuration

Inside the root of the `test_wrappers` directory make sure that the configuration file is defined:

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

Tbd. 