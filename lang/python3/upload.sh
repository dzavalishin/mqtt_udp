#!/bin/sh

# test site upload
#twine upload --repository-url https://test.pypi.org/legacy/ dist/*

# main site upload
twine upload --repository-url https://upload.pypi.org/legacy/ dist/*

