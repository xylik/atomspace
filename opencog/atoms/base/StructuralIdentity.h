/*
 * opencog/atoms/base/StructuralIdentity.h
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

#ifndef _OPENCOG_STRUCTURAL_IDENTITY_H
#define _OPENCOG_STRUCTURAL_IDENTITY_H

#include <string>
#include <sstream>
#include <opencog/atoms/base/Atom.h>
#include <opencog/atoms/base/Node.h>
#include <opencog/atoms/base/Link.h>

namespace opencog
{

/**
 * Utility class for computing structural identity keys
 * that provide content-addressed global uniqueness.
 * 
 * This implements the structural identity refactor mentioned 
 * in the review feedback, replacing hashCode-based interning
 * with canonical structural key strings.
 */
class StructuralIdentity
{
public:
    /**
     * Compute structural key for a Node.
     * Format: "N|<type_name>|<node_name>"
     */
    static std::string compute_node_key(const Node* node);
    
    /**
     * Compute structural key for a Link.
     * Format: "L|<type_name>|<arity>|<child1_key>|<child2_key>|..."
     */
    static std::string compute_link_key(const Link* link);
    
    /**
     * Compute structural key for any Atom.
     * Dispatches to appropriate method based on atom type.
     */
    static std::string compute_atom_key(const Atom* atom);
    
    /**
     * Compute structural key for a Handle.
     */
    static std::string compute_handle_key(const Handle& handle);
    
    /**
     * Compute hash from structural key.
     * This can be used to replace standard hash computation
     * with content-addressed hashing.
     */
    static ContentHash hash_from_structural_key(const std::string& key);
    
    /**
     * Check if two atoms have the same structural identity.
     */
    static bool structural_equal(const Atom* a, const Atom* b);
    
private:
    static std::string escape_string(const std::string& str);
};

} // namespace opencog

#endif // _OPENCOG_STRUCTURAL_IDENTITY_H