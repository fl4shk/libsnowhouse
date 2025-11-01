#include "BaseDerived.hpp"

Base::Base(size_t s_data_size) {
	for (size_t i=0; i<s_data_size; ++i) {
		data.push_back(0x0);
	}
}
Base& Base::consume(
	const Base& other
) {
	//for (auto& item: data) {
	//	
	//}
	for (size_t i=0; i<data.size(); ++i) {
		data.at(i) += other.data.at(i);
	}
	return *this;
}

Base& Derived::consume(
	const Base& other
) {
	for (size_t i=0; i<data.size(); ++i) {
		data.at(i) -= other.data.at(i);
	}
	return *this;
}

Derived a, b;
