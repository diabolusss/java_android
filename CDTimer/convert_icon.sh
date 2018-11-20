#!/bin/bash
        #convert $i -resize 50% -quality 25% converted/$(basename $i .$image_type).jpg
        #convert $i -resize 50% -quality 25% $(basename $i .$image_type).jpg
        #100x75 randori preview
        #1024x760
#mkdir converted 
out_name="stopwatch_"
in_name="stopwatch"

convert $in_name.png -resize 36x36 -quality 100% "$out_name"36x36ldpi.png
convert $in_name.png -resize 48x48 -quality 100% "$out_name"48x48mdpi.png
convert $in_name.png -resize 50x50 -quality 100% "$out_name"50x50bada.png
convert $in_name.png -resize 57x57 -quality 100% "$out_name"57x57ios.png
convert $in_name.png -resize 64x64 -quality 100% "$out_name"64x64webos.png
convert $in_name.png -resize 72x72 -quality 100% "$out_name"72x72hdpi.png
convert $in_name.png -resize 80x80 -quality 100% "$out_name"80x80blackberry.png
convert $in_name.png -resize 96x96 -quality 100% "$out_name"96x96xhdpi.png
convert $in_name.png -resize 114x114 -quality 100% "$out_name"114x114ios.png
convert $in_name.png -resize 128x128 -quality 100% "$out_name"128x128bada.png
convert $in_name.png -resize 144x144 -quality 100% "$out_name"144x144xxhdpi.png
convert $in_name.png -resize 173x173 -quality 100% "$out_name"173x173winphone.png
convert $in_name.png -resize 192x192 -quality 100% "$out_name"192x192xxxhdpi.png
convert $in_name.png -resize 512x512 -quality 100% "$out_name"512x512googleplaystore.png
