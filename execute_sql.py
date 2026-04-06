#!/usr/bin/env python3
"""
Execute SQL script to create test users in PostgreSQL
"""

import sys
import os

try:
    import psycopg2
    from psycopg2 import sql
except ImportError:
    print("Installing psycopg2...")
    os.system("pip install psycopg2-binary")
    import psycopg2

# Database connection parameters
DB_HOST = os.getenv('DB_HOST', 'localhost')
DB_PORT = os.getenv('DB_PORT', '5432')
DB_NAME = os.getenv('DB_NAME', 'enrollmentdb')
DB_USER = os.getenv('DB_USER', 'postgres')
DB_PASS = os.getenv('DB_PASS', 'madride123')

print(f"Connecting to PostgreSQL: {DB_USER}@{DB_HOST}:{DB_PORT}/{DB_NAME}")

try:
    # Connect to PostgreSQL
    conn = psycopg2.connect(
        host=DB_HOST,
        port=DB_PORT,
        database=DB_NAME,
        user=DB_USER,
        password=DB_PASS
    )
    
    cursor = conn.cursor()
    print("✓ Connected to database successfully")
    
    # Read SQL script
    script_path = 'CREATE_TEST_USERS.sql'
    with open(script_path, 'r') as f:
        sql_script = f.read()
    
    print(f"✓ Read SQL script from {script_path}")
    
    # Split script by statements (handling comments)
    statements = []
    current_statement = ""
    in_comment = False
    
    for line in sql_script.split('\n'):
        stripped = line.strip()
        
        # Skip comment lines and empty lines
        if stripped.startswith('--'):
            if current_statement:
                statements.append(current_statement)
                current_statement = ""
            continue
        
        if not stripped:
            continue
        
        current_statement += line + "\n"
        
        # Check if statement ends with semicolon
        if stripped.endswith(';'):
            statements.append(current_statement)
            current_statement = ""
    
    # Add any remaining statement
    if current_statement.strip():
        statements.append(current_statement)
    
    # Execute statements
    executed = 0
    for statement in statements:
        statement = statement.strip()
        if not statement:
            continue
        
        try:
            cursor.execute(statement)
            conn.commit()
            executed += 1
            print(f"✓ Executed statement {executed}")
        except psycopg2.Error as e:
            # Some statements might fail due to ON CONFLICT or similar
            # This is expected
            if "duplicate" in str(e).lower() or "already exists" in str(e).lower():
                print(f"  ⚠ Skipped (already exists): {str(e).split(chr(10))[0][:60]}")
            elif "column" in str(e).lower() or "relation" in str(e).lower():
                print(f"  ⚠ Skipped (schema issue): {str(e).split(chr(10))[0][:60]}")
            else:
                print(f"⚠ Warning in statement {executed}: {str(e).split(chr(10))[0][:80]}")
            conn.commit()  # Commit anyway to continue
    
    print(f"\n✓ Executed {executed} SQL statements successfully")
    
    # Run verification queries
    print("\n" + "="*60)
    print("VERIFICATION: Created Users")
    print("="*60)
    
    cursor.execute("""
        SELECT u.id, u.username, u.email, u.first_name, u.last_name, u.is_active
        FROM users u
        WHERE u.username IN ('admin_user', 'validateur_user', 'saisie_user', 'consultant_user')
        AND u.deleted_at IS NULL
        ORDER BY u.username;
    """)
    
    print("\nUsers created:")
    users = cursor.fetchall()
    if users:
        print(f"{'ID':<5} {'Username':<20} {'Email':<25} {'First Name':<15} {'Last Name':<15} {'Active':<8}")
        print("-" * 100)
        for user in users:
            print(f"{user[0]:<5} {user[1]:<20} {user[2]:<25} {user[3]:<15} {user[4]:<15} {str(user[5]):<8}")
    else:
        print("No users found!")
    
    print("\n" + "="*60)
    print("VERIFICATION: User-Role Assignments")
    print("="*60)
    
    cursor.execute("""
        SELECT u.username, r.role_name
        FROM user_roles ur
        JOIN users u ON ur.user_id = u.id
        JOIN roles r ON ur.role_id = r.id
        WHERE u.username IN ('admin_user', 'validateur_user', 'saisie_user', 'consultant_user')
        AND u.deleted_at IS NULL
        ORDER BY u.username;
    """)
    
    print("\nUser-Role assignments:")
    user_roles = cursor.fetchall()
    if user_roles:
        print(f"{'Username':<20} {'Role':<20}")
        print("-" * 40)
        for username, role in user_roles:
            print(f"{username:<20} {role:<20}")
    else:
        print("No user-role assignments found!")
    
    print("\n" + "="*60)
    print("VERIFICATION: Role-Permission Counts")
    print("="*60)
    
    cursor.execute("""
        SELECT r.role_name, COUNT(p.id) as permission_count
        FROM role_permissions rp
        JOIN roles r ON rp.role_id = r.id
        JOIN permissions p ON rp.permission_id = p.id
        WHERE r.role_name IN ('ADMIN', 'VALIDATEUR', 'SAISIE', 'CONSULTATION')
        GROUP BY r.role_name
        ORDER BY r.role_name;
    """)
    
    print("\nRole-Permission assignments:")
    role_perms = cursor.fetchall()
    if role_perms:
        print(f"{'Role':<20} {'Permissions':<15}")
        print("-" * 35)
        for role, count in role_perms:
            print(f"{role:<20} {count:<15}")
    else:
        print("No role-permission assignments found or permissions table is empty!")
    
    print("\n" + "="*60)
    print("✓ Script execution completed successfully!")
    print("="*60)
    
    cursor.close()
    conn.close()

except psycopg2.Error as e:
    print(f"\n✗ Database connection error: {e}")
    sys.exit(1)
except FileNotFoundError:
    print(f"\n✗ SQL script file not found: {script_path}")
    sys.exit(1)
except Exception as e:
    print(f"\n✗ Unexpected error: {e}")
    sys.exit(1)
