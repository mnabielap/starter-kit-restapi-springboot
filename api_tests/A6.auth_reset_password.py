import sys
import os
sys.path.append(os.path.abspath(os.path.dirname(__file__)))
import utils

# --- MANUAL INPUT REQUIRED HERE ---
# Paste the token from the email link (e.g., ...?token=UUID)
TOKEN_FROM_EMAIL = "PASTE_YOUR_UUID_TOKEN_HERE" 
# ----------------------------------

URL = f"{utils.BASE_URL}/auth/reset-password?token={TOKEN_FROM_EMAIL}"
PAYLOAD = {
    "password": "newpassword123"
}

utils.send_and_print(
    url=URL,
    method="POST",
    body=PAYLOAD,
    output_file=f"{os.path.splitext(os.path.basename(__file__))[0]}.json",
)