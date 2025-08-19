/*
 * StructuralIdentity.cc
 *
 * Copyright (C) 2024 OpenCog Foundation
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

#include "StructuralIdentity.h"
#include <opencog/atoms/atom_types/NameServer.h>

using namespace opencog;

std::string StructuralIdentity::compute_node_key(const Node* node)
{
    if (!node) return "N|NULL|NULL";
    
    std::stringstream ss;
    ss << "N|" 
       << nameserver().getTypeName(node->get_type()) 
       << "|" 
       << escape_string(node->get_name());
    return ss.str();
}

std::string StructuralIdentity::compute_link_key(const Link* link)
{
    if (!link) return "L|NULL|0";
    
    std::stringstream ss;
    ss << "L|" 
       << nameserver().getTypeName(link->get_type()) 
       << "|" 
       << link->get_arity();
    
    // Add child keys in order
    for (const Handle& child : link->getOutgoingSet()) {
        ss << "|" << compute_handle_key(child);
    }
    
    return ss.str();
}

std::string StructuralIdentity::compute_atom_key(const Atom* atom)
{
    if (!atom) return "NULL|NULL|NULL";
    
    if (atom->is_node()) {
        return compute_node_key(static_cast<const Node*>(atom));
    } else if (atom->is_link()) {
        return compute_link_key(static_cast<const Link*>(atom));
    } else {
        // Fallback for other atom types
        std::stringstream ss;
        ss << "A|" 
           << nameserver().getTypeName(atom->get_type()) 
           << "|" 
           << atom->id_to_string();
        return ss.str();
    }
}

std::string StructuralIdentity::compute_handle_key(const Handle& handle)
{
    if (!handle) return "HANDLE|NULL";
    return compute_atom_key(handle.get());
}

ContentHash StructuralIdentity::hash_from_structural_key(const std::string& key)
{
    ContentHash hsh = std::hash<std::string>()(key);
    
    // Ensure proper bit patterns for nodes vs links
    if (key.substr(0, 2) == "N|") {
        // Nodes will never have the MSB set
        ContentHash mask = ~(((ContentHash) 1ULL) << (8*sizeof(ContentHash) - 1));
        hsh &= mask;
    } else if (key.substr(0, 2) == "L|") {
        // Links will always have the MSB set
        ContentHash mask = ((ContentHash) 1ULL) << (8*sizeof(ContentHash) - 1);
        hsh |= mask;
    }
    
    if (Handle::INVALID_HASH == hsh) hsh -= 1;
    return hsh;
}

bool StructuralIdentity::structural_equal(const Atom* a, const Atom* b)
{
    if (a == b) return true;
    if (!a || !b) return false;
    
    return compute_atom_key(a) == compute_atom_key(b);
}

std::string StructuralIdentity::escape_string(const std::string& str)
{
    std::string result;
    result.reserve(str.length() + 20); // Reserve some extra space for escaping
    
    for (char c : str) {
        switch (c) {
            case '|':
                result += "\\|";
                break;
            case '\\':
                result += "\\\\";
                break;
            case '\n':
                result += "\\n";
                break;
            case '\r':
                result += "\\r";
                break;
            case '\t':
                result += "\\t";
                break;
            default:
                result += c;
                break;
        }
    }
    
    return result;
}

/* ===================== END OF FILE ===================== */