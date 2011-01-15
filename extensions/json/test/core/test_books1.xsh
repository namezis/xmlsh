import commands posix
import module j=json

OUT=$(mktemp -d)

cd ../../samples


j:jsonxslt -o $OUT -v -xsd books.xsd > $OUT/all.xml 
xslt -f $OUT/tojson.xsl < books.xml > $OUT/books.jxml
xsdvalidate ../schemas/jxon.xsd $OUT/books.jxml && xml2json -p < $OUT/books.jxml > $OUT/books.json

json2xml < $OUT/books.json | xtee $OUT/temp.jxon | xslt -f $OUT/toxml.xsl > $OUT/temp.xml

# cat $OUT/temp.xml

if xcmp -x -b books.xml $OUT/temp.xml ; then
        echo Round trip succeeded
else
        echo Round trip not valid
fi

# echo $OUT
rm -r -f $OUT