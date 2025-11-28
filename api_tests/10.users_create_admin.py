import sys
import os
sys.path.append(os.path.abspath(os.path.dirname(__file__)))
import utils

# Load Access Token
access_token = utils.load_config("accessToken")

if not access_token:
    print("[ERROR] No access token found. Please login.")
else:
    URL = f"{utils.BASE_URL}/users"
    HEADERS = {
        "Authorization": f"Bearer {access_token}"
    }
    PAYLOAD = {
        "name": "Admin Created User",
        "email": "admin.created@example.com",
        "password": "password123",
        "role": "USER" # or ADMIN
    }

    utils.send_and_print(
        url=URL,
        method="POST",
        headers=HEADERS,
        body=PAYLOAD,
        output_file=f"{os.path.splitext(os.path.basename(__file__))[0]}.json",
    )