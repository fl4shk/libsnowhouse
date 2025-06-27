for file in test-*-results-*.txt; do
	echo $file
	diff ../known-good-results/$file $file
done
