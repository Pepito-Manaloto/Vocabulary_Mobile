branch=$(git rev-parse --abbrev-ref HEAD)
count=$(git log --oneline | wc -l | gawk '{print $1}')
revision=$(git rev-parse --short HEAD)

dash=\&#8211\;
version=$branch$dash$count$dash$revision

echo "Version: $version";

string_xml=$(pwd)'/res/values/strings.xml'

#sed -i "s/<string name=\"build_num\">*<\/string>/<string name=\"build_num\">$version<\/string>/g" "$string_xml";