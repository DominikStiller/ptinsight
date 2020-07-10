from setuptools import setup, find_namespace_packages

setup(
    name="ptinsight-common",
    version="1.0",
    packages=find_namespace_packages(include=["ptinsight.*"]),
    url="",
    license="",
    author="Dominik Stiller",
    author_email="dominik.stiller@hpe.com",
    description="Common functionality for PT Insight Python components",
    install_requires=["protobuf >= 3", "h3 >= 3.6"],
)
