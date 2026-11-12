package org.bcp.dlmsenrolment.security;

import java.util.Set;

/**
 * Profile (role) names used by the application.
 *
 * <ul>
 *   <li>{@link #ADMIN_CENTRAL} - full administration (users, roles, permissions, audit).</li>
 *   <li>{@link #AGENT_HABILITATION} - user management and role assignment only; may only grant
 *       {@link #ASSIGNABLE_BY_HABILITATION}.</li>
 *   <li>{@link #AGENT_ENROLEMENT} - data entry + validation of partners/clients. The 4-eyes rule
 *       (the validator must not be the submitter) is enforced in PartnerValidationServiceImpl.</li>
 *   <li>{@link #CONSULTATION} - read-only.</li>
 * </ul>
 *
 * {@link #LEGACY_SAISIE} and {@link #LEGACY_VALIDATEUR} were merged into {@link #AGENT_ENROLEMENT};
 * they are kept in the database (inactive) only for history.
 */
public final class Profiles {

    public static final String ADMIN_CENTRAL = "PROFIL_ADMIN_CENTRAL";
    public static final String AGENT_HABILITATION = "PROFIL_AGENT_HABILITATION";
    public static final String AGENT_ENROLEMENT = "PROFIL_AGENT_ENROLEMENT";
    public static final String CONSULTATION = "PROFIL_CONSULTATION";

    public static final String LEGACY_SAISIE = "PROFIL_SAISIE";
    public static final String LEGACY_VALIDATEUR = "PROFIL_VALIDATEUR";

    /** Roles an habilitation agent is allowed to assign or revoke. */
    public static final Set<String> ASSIGNABLE_BY_HABILITATION = Set.of(AGENT_ENROLEMENT, CONSULTATION);

    /** Accounts an habilitation agent may not modify, deactivate or change roles of. */
    public static final Set<String> PROTECTED_FROM_HABILITATION = Set.of(ADMIN_CENTRAL, AGENT_HABILITATION);

    private Profiles() {
    }
}
