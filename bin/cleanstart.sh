#!/bin/sh

freshet_version=0.1.0-SNAPSHOT

home_dir=`pwd`

base_dir=$(dirname $0)/..

cd $base_dir
base_dir=`pwd`
cd $home_dir

username=$(whoami)

# Build and Install Freshet Core
cd $base_dir/freshet-core
lein install

cd $base_dir/freshet-dsl
lein install

# Clean Freshet Job Package
cd $base_dir/freshet-job-package
mvn clean

# Run Freshet Shell after local setup
cd $base_dir/freshet-shell
./bin/setup.sh local
./bin/fshell
