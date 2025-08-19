/*
 * opencog/atoms/foreign/JavaAST.h
 *
 * Copyright (C) 2024 OpenCog Foundation
 * All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License v3 as
 * published by the Free Software Foundation and including the exceptions
 * at http://opencog.org/wiki/Licenses
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to:
 * Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

#ifndef _OPENCOG_JAVA_AST_H
#define _OPENCOG_JAVA_AST_H

#include <opencog/atoms/foreign/ForeignAST.h>

namespace opencog
{
/** \addtogroup grp_atomspace
 *  @{
 */

/// The JavaAST holds Java abstract syntax trees
/// parsed from Java source code and represented as
/// canonical structural key strings for content-addressable
/// global uniqueness.
class JavaAST : public ForeignAST
{
	void init();

protected:
	void parse(const std::string&);
	std::string compute_structural_key() const;

public:
	JavaAST(const HandleSeq&&, Type = JAVA_AST);
	JavaAST(const HandleSeq&&, const std::string&&);
	JavaAST(const JavaAST&) = delete;
	JavaAST& operator=(const JavaAST&) = delete;

	JavaAST(const std::string&);

	virtual std::string to_string(const std::string& indent) const;
	virtual std::string to_short_string(const std::string& indent) const;

	static Handle factory(const Handle&);

protected:
	virtual ContentHash compute_hash() const override;
	virtual std::string get_structural_key() const;
};

LINK_PTR_DECL(JavaAST)
#define createJavaAST CREATE_DECL(JavaAST)

/** @}*/
}

#endif // _OPENCOG_JAVA_AST_H