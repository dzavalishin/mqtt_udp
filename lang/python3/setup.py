#!/bin/python

from setuptools import setup, find_packages


with open("README.md", "r") as fh:
    f_long_description = fh.read()

setup(
    name='mqttudp',
    version='0.1-1',
    description='An MQTT/UDP implementation in Python',
    long_description=f_long_description,
    long_description_content_type="text/markdown",
    author='Dmitry Zavalishin',
    author_email='dz@dz.ru',
    url='https://github.com/dzavalishin/mqtt_udp',
    packages=find_packages(),
    #platforms='any',
    #zip_safe=False,
    include_package_data=True,
    #install_requires=['admintools==0.1'],
    classifiers=[
        #'Development Status :: 5 - Production/Stable',
        'Development Status :: 4 - Beta',
        'Environment :: Console',
        'Intended Audience :: Developers',
        'Operating System :: OS Independent',
        'Programming Language :: Python',
        'Topic :: Internet',
        'Topic :: System :: Networking',
    ],
    #data_files=[(
    #      '/path/to/icons', ['myApp/icons/a.ico', 'myApp/icons/e.ico']
    #)],
)

