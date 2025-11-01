#include "BaseDerived.hpp"

int main(int argc, char** argv) {
	for (size_t i=0; i<a.data.size(); ++i) {
		a.data.at(i) = (i + 1) * 2;
	}
	for (size_t i=0; i<b.data.size(); ++i) {
		b.data.at(i) = i;
	}

	a.consume(b);

	int my_sum = 0;
	for (const auto& item: a.data) {
		my_sum += item;
	}

	return my_sum;
}
