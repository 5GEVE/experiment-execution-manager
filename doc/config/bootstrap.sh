#!/bin/bash

usage() { 
	echo "Usage: $0 -c cataloguehost -m msnoHost" 1>&2; 
	echo "Example: $0 -c 127.0.0.1:8082 -m 127.0.0.1:8085"
	exit 1; 
}

while getopts ":c:m:" o; do
    case "${o}" in
		c)
			c=${OPTARG}
			;;	
		m)
			m=${OPTARG}
			;;
		*)
            usage
            ;;
    esac
done
shift $((OPTIND-1))

if [ -z "${c}" ] || [ -z "${m}"]; then
    usage
fi

currdir=$(pwd)
export MAVEN_HOME=/opt/maven/current
export PATH=$PATH:$MAVEN_HOME/bin

##Configuring database
echo -e "Database configuration"
sudo -u postgres psql -c "CREATE DATABASE eemdb"
sudo -u postgres psql -U postgres -d eemdb -c "alter user postgres with password 'postgres';"

## Create log directory
echo -e "Creating log directory in /var/log/"
sudo mkdir -p /var/log/eem/
sudo chown $USER:$USER /var/log/eem/

## Create packages directory which will contain jar packages
echo -e "Creating configurations directory in /home/$USER/"
mkdir -p /home/$USER/EEM/target/

## Move application properties to configuration directory
echo -e "Copying application.properties of EEM to the configs directory"
cp ${currdir}/../../eem/src/main/resources/application.properties.template /home/$USER/EEM/application.properties

## Change data on application properties
echo -e "Parameterizing the application.properties with the provided data"
sed -i "s|_CATALOGUE_|"${c}"|g" /home/$USER/EEM/application.properties
sed -i "s|_MSNO_|"${m}"|g" /home/$USER/EEM/application.properties

## To avoid compile issue
cp /home/$USER/EEM/application.properties ${currdir}/../../eem/src/main/resources/application.properties

## Building EEM package
echo -e "Building EEM package"
cd ${currdir}/../../eem/ && mvn -DskipTests clean package
if [[ "$?" -ne 0 ]] ; then
  echo 'could not perform packaging of EEM'; exit $rc
fi

## Moving jar to running directory
echo -e "Moving package to packages directory"
cp ${currdir}/../../eem/target/experiment-execution-manager-1.0.0.jar /home/$USER/EEM/target/.

## Create service file for EEM
echo "[Unit]" > /home/$USER/EEM/eem.service
echo "Description=5GEve EEM" >> /home/$USER/EEM/eem.service
echo "After=syslog.target network-online.target" >> /home/$USER/EEM/eem.service
echo "" >> /home/$USER/EEM/eem.service
echo "[Service]" >> /home/$USER/EEM/eem.service
echo "User=${USER}" >> /home/$USER/EEM/eem.service
echo "Restart=on-failure" >> /home/$USER/EEM/eem.service
echo "ExecStart=/usr/bin/java -jar /home/$USER/EEM/target/experiment-execution-manager-1.0.0.jar --spring.config.location=file:/home/$USER/EEM/" >> /home/$USER/EEM/eem.service
echo "RestartSec=3" >> /home/$USER/EEM/eem.service
echo "" >> /home/$USER/EEM/eem.service
echo "[Install]" >> /home/$USER/EEM/eem.service
echo "WantedBy=multi-user.target" >> /home/$USER/EEM/eem.service


## Linking file to the services list
echo -e "Linking eem.service to services"
sudo systemctl enable /home/$USER/EEM/eem.service
sudo systemctl start eem.service

echo -e "In order to activate/deactivate the service run: \n$ sudo systemctl start|stop eem.service"

exit 0


