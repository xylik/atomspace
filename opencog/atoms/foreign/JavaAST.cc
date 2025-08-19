/*
 * JavaAST.cc
 *
 * Copyright (C) 2024 OpenCog Foundation
 *
 * Author: OpenCog Coding AI Agent
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License v3 as
 * published by the Free Software Foundation and including the
 * exceptions at http://opencog.org/wiki/Licenses
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this program; if not, write to:
 * Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

#include "JavaAST.h"
#include <sstream>
#include <algorithm>

using namespace opencog;

void JavaAST::init()
{
	if (not nameserver().isA(_type, JAVA_AST))
	{
		const std::string& tname = nameserver().getTypeName(_type);
		throw InvalidParamException(TRACE_INFO,
			"Expecting a JavaAST, got %s", tname.c_str());
	}
}

JavaAST::JavaAST(const HandleSeq&& oset, Type t)
	: ForeignAST(std::move(oset), t)
{
	init();
}

JavaAST::JavaAST(const HandleSeq&& oset, const std::string&& java_code)
	: ForeignAST(std::move(oset), JAVA_AST)
{
	init();
	_name = java_code;
}

JavaAST::JavaAST(const std::string& java_code)
	: ForeignAST(JAVA_AST)
{
	parse(java_code);
}

// ---------------------------------------------------------------

/// Basic Java code parsing - this is a simplified parser
/// that creates a structural representation of Java code.
/// In a full implementation, this would use a proper Java parser
/// like JavaParser or ANTLR.
void JavaAST::parse(const std::string& java_code)
{
	// For now, store the raw Java code
	// TODO: Implement proper Java AST parsing
	_name = java_code;
	
	// This is a stub implementation - a real parser would:
	// 1. Tokenize the Java code
	// 2. Build an AST structure
	// 3. Create child JavaAST nodes for sub-expressions
	// 4. Set the structural key based on the parsed structure
}

// ---------------------------------------------------------------

/// Compute a structural key for content-addressed identity
/// Format: "J|<type>|<structure_hash>"
std::string JavaAST::compute_structural_key() const
{
	std::stringstream ss;
	ss << "J|" << nameserver().getTypeName(_type) << "|";
	
	if (_outgoing.empty()) {
		// For leaf nodes, use the name content
		ss << "LEAF|" << _name;
	} else {
		// For nodes with children, include arity and child keys
		ss << "ARITY|" << _outgoing.size() << "|";
		for (const Handle& child : _outgoing) {
			if (JavaASTCast(child)) {
				ss << JavaASTCast(child)->get_structural_key() << "|";
			} else {
				ss << child->id_to_string() << "|";
			}
		}
	}
	
	return ss.str();
}

std::string JavaAST::get_structural_key() const
{
	return compute_structural_key();
}

/// Override hash computation to use structural keys
/// This implements the structural identity refactor
ContentHash JavaAST::compute_hash() const
{
	std::string structural_key = compute_structural_key();
	ContentHash hsh = std::hash<std::string>()(structural_key);
	
	// Links will always have the MSB set.
	ContentHash mask = ((ContentHash) 1ULL) << (8*sizeof(ContentHash) - 1);
	hsh |= mask;
	
	if (Handle::INVALID_HASH == hsh) hsh -= 1;
	return hsh;
}

// ---------------------------------------------------------------

std::string JavaAST::to_string(const std::string& indent) const
{
	if (0 == _outgoing.size())
		return indent + "(JavaAST \"" + _name + "\") ; " + id_to_string();

	std::string rv = indent + "(JavaAST\n";
	for (const Handle& h: _outgoing)
		rv += h->to_string(indent + "  ") + "\n";

	rv += indent + ") ; " + id_to_string();
	return rv;
}

std::string JavaAST::to_short_string(const std::string& indent) const
{
	if (0 == _outgoing.size())
	{
		if (0 != indent.size()) return _name;
		return _name + "\n" + to_string(";") + "\n";
	}

	std::string rv = "java{";
	for (const Handle& h: _outgoing)
	{
		if (JAVA_AST == h->get_type())
			rv += h->to_short_string("xx") + " ";
		else
			rv += h->to_short_string("");
	}
	
	if (rv.size() > 5) rv[rv.size()-1] = '}';
	else rv += "}";

	// Debugging print
	if (0 == indent.size()) rv += "\n" + to_string(";") + "\n";
	return rv;
}

// ---------------------------------------------------------------
// Custom factory, following the pattern of DatalogAST and SexprAST

Handle JavaAST::factory(const Handle& base)
{
	/* If it's castable, nothing to do. */
	if (JavaASTCast(base)) return base;

	if (0 == base->get_arity())
		return HandleCast(createJavaAST(std::move(base->get_name())));

	// For links, serialize the structure back to Java code representation
	std::string java_repr = "/* Generated from AtomSpace structure */";
	return HandleCast(createJavaAST(
		std::move(base->getOutgoingSet()),
		std::move(java_repr)));
}

/* This runs when the shared lib is loaded. */
static __attribute__ ((constructor)) void init_javaast_factory(void)
{
	classserver().addFactory(JAVA_AST, &JavaAST::factory);
}

/* ===================== END OF FILE ===================== */