#ifndef BASE_DERIVED_HPP
#define BASE_DERIVED_HPP

#include <stdint.h>
#include <stdlib.h>
#include <array>
#include <vector>

class Base {
public:		// variables
	//std::array<int, 8> data;
	std::vector<int> data;
public:		// functions
	Base(size_t s_data_size=10);
	virtual Base& consume(const Base& other);
};

class Derived: public Base {
public:		// functions
	virtual Base& consume(const Base& other);
};

extern Derived a, b;
#endif		// BASE_DERIVED_HPP
