package edu.iitm.jamunaconnect.domain;

/**
 * Matches how residents actually report issues in the hostel groups: water
 * (taste, filters, coolers, restroom plumbing), power, LAN/internet, cleaning
 * (handwash, dustbins, flushing), maintenance (nets, mirrors, furniture),
 * pests (monkeys, mosquitoes), and lost &amp; found.
 */
public enum ComplaintCategory {
    WATER,
    ELECTRICAL,
    INTERNET,
    CLEANING,
    MAINTENANCE,
    PEST_CONTROL,
    LOST_AND_FOUND,
    OTHER
}