import sys
import os
sys.path.append(os.path.abspath(os.path.dirname(__file__)))
import utils

# Load Access Token
access_token = utils.load_config("accessToken")
TARGET_USER_ID = 1

if not access_token:
    print("[ERROR] No access token found. Please login.")
else:
    URL = f"{utils.BASE_URL}/users/{TARGET_USER_ID}"
    HEADERS = {
        "Authorization": f"Bearer {access_token}"
    }
    PAYLOAD = {
        "name": "John Updated"
    }

    utils.send_and_print(
        url=URL,
        method="PATCH",
        headers=HEADERS,
        body=PAYLOAD,
        output_file=f"{os.path.splitext(os.path.basename(__file__))[0]}.json",
    )