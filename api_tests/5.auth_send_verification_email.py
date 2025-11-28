import sys
import os
sys.path.append(os.path.abspath(os.path.dirname(__file__)))
import utils

# Load Access Token
access_token = utils.load_config("accessToken")

if not access_token:
    print("[ERROR] No access token found in secrets.json. Please login first.")
else:
    # Endpoint Configuration
    URL = f"{utils.BASE_URL}/auth/send-verification-email"
    HEADERS = {
        "Authorization": f"Bearer {access_token}"
    }

    # Execute Request
    utils.send_and_print(
        url=URL,
        method="POST",
        headers=HEADERS,
        output_file=f"{os.path.splitext(os.path.basename(__file__))[0]}.json",
    )