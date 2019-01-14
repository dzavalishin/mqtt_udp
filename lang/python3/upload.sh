#!/bin/sh

# see https://packaging.python.org/guides/migrating-to-pypi-org/
# see https://pypi.org/project/mqttudp/

# test site upload - https://test.pypi.org/manage/project/mqttudp/releases/
#twine upload --repository-url https://test.pypi.org/legacy/ dist/*

# main site upload
twine upload --repository-url https://upload.pypi.org/legacy/ dist/*

