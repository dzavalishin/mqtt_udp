#!/bin/sh
#./setup.py sdist upload
#python ./setup.py sdist upload -r https://upload.pypi.org/legacy/
#python3 setup.py sdist bdist_wheel

twine upload --repository-url https://test.pypi.org/legacy/ dist/*
