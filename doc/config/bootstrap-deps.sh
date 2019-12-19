#!/bin/bash

cd /home/$USER/

## Update source lists
sudo apt update

echo "Install git and systemd"
sudo apt install systemd git -y

## Installing Open JDK
echo -e "Installing Open JDK 1.8"
sudo add-apt-repository ppa:openjdk-r/ppa -y
sudo apt update
sudo apt-get install openjdk-8-jdk -y

## Installing maven
echo -e "Installing Maven 3.3.9"
wget http://apache-mirror.rbc.ru/pub/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
tar -xvzpf apache-maven-3.3.9-bin.tar.gz
sudo mkdir -p /opt/maven/3.3.9
sudo mv apache-maven-3.3.9/* /opt/maven/3.3.9/
sudo ln -s /opt/maven/3.3.9/ /opt/maven/current
echo 'export MAVEN_HOME=/opt/maven/current' >> ~/.bashrc
echo 'export PATH=$PATH:$MAVEN_HOME/bin' >> ~/.bashrc
export MAVEN_HOME=/opt/maven/current
export PATH=$PATH:$MAVEN_HOME/bin

## installing postgres
echo -e "Installing Postgres Server"
sudo apt install postgresql postgresql-contrib -y

## installing rabbitmg
echo -e "Installing RabbitMq Server"
sudo apt install rabbitmq-server -y

## installing nfv-ifa-libs
git clone https://github.com/nextworks-it/nfv-ifa-libs.git
cd  nfv-ifa-libs
##to be changed
git checkout feat-librefactor
cd NFV_MANO_LIBS_COMMON
mvn clean install
cd ../NFV_MANO_LIBS_DESCRIPTORS
mvn clean install
cd ../NFV_MANO_LIBS_CATALOGUES_IF
mvn clean install
cd ../..

## installing slicer-catalogue
git clone https://github.com/nextworks-it/slicer-catalogue.git
cd slicer-catalogue
git checkout 5geve-release
cd VS_BLUEPRINTS_IM
mvn clean install
cd ../EVE_BLUEPRINTS_IM
mvn clean install
cd ../VS_BLUEPRINTS_CATALOGUE_INTERFACES
mvn clean install
cd ../EVE_BLUEPRINTS_CATALOGUE_INTERFACES
mvn clean install
cd ../TranslatorServiceInterface
mvn clean install

## installing nfvo-drivers
git clone https://github.com/nextworks-it/nfvo-drivers.git
cd nfvo-drivers/msnoClient
mvn clean install

exit 0
