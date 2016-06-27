npm install
npm run build

DEST="../entity-browser-app/src/main/webapp"
echo "deleting existing resources in " $DEST
rm $DEST/index.html
find $DEST -type f -name 'main*.js' -delete
find $DEST -type f -name 'vendor*.js' -delete

echo "creating existing resources"
cp dist/index.html $DEST
cp -p $(find dist/ -name '*.js') $DEST

