#!/bin/sh

freshet_version=0.1.0-SNAPSHOT

home_dir=`pwd`


base_dir=$(dirname $0)/..


cd $base_dir
base_dir=`pwd`
cd $home_dir

username=$(whoami)
COMMAND=$1

freshet_job_package_parent="$(dirname $(readlink -e $base_dir/../freshet-job-package))/$(basename $base_dir/../freshet-job-package)"
freshet_job_package="$freshet_job_package_parent/target/freshet-job-package-$freshet_version-dist.tar.gz"

echo "Parent directory of Freshet Job Package: $freshet_job_package_parent"
echo "Freshet Job Package Path: $freshet_job_package"

function install() {

  # Setting up YARN, Kafka and Zookeeper
  $base_dir/bin/grid bootstrap

  if [ ! -f "$freshet_job_package" ]; then
    cd $freshet_job_package_parent
    mvn clean package
  fi

  if [ -f "$freshet_job_package" ]; then
    # Extracting Job Package to 'deploy/freshet'
    mkdir -p $base_dir/deploy/freshet
    tar xvf $freshet_job_package -C $base_dir/deploy/freshet

    # Upload the Freshet Job Package to HDFS
    #$base_dir/deploy/yarn/bin/hdfs dfs -mkdir /user
    #$base_dir/deploy/yarn/bin/hdfs dfs -mkdir /user/$username
    $base_dir/deploy/yarn/bin/hdfs dfs -mkdir /freshet

    # TODO: Do this programmatically at shell startup
    $base_dir/deploy/yarn/bin/hdfs dfs -put $freshet_job_package /freshet

    mkdir -p $base_dir/deploy/freshet/conf

    echo "yarn.package.path=hdfs://localhost:9000/freshet/$(basename $freshet_job_package)" > $base_dir/deploy/freshet/conf/freshet.conf
  else
    echo "Cannot find $freshet_job_package. Looks like freshet-job-package build failed."
  fi
}

if [ "$COMMAND" == "local" ]; then
  install
  exit 0
else
  echo "Unknown command: $COMMAND"
fi







