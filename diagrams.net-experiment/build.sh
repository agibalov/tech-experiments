DRAWIO="/Applications/draw.io.app/Contents/MacOS/draw.io"
rm -rf build
mkdir build
${DRAWIO} --export --format png --output build/ src/
${DRAWIO} --export --format png --page-index 0 --output build/hello-world-page0.png src/hello-world.drawio
${DRAWIO} --export --format png --page-index 1 --output build/hello-world-page1.png src/hello-world.drawio
