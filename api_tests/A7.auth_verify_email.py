import sys
import os
sys.path.append(os.path.abspath(os.path.dirname(__file__)))
import utils

# --- MANUAL INPUT REQUIRED HERE ---
# Paste the token from the email link
TOKEN_FROM_EMAIL = "PASTE_YOUR_UUID_TOKEN_HERE" 
# ----------------------------------

URL = f"{utils.BASE_URL}/auth/verify-email?token={TOKEN_FROM_EMAIL}"

utils.send_and_print(
    url=URL,
    method="POST",
    output_file=f"{os.path.splitext(os.path.basename(__file__))[0]}.json",
)