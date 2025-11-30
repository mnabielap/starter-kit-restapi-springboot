import sys
import os
sys.path.append(os.path.abspath(os.path.dirname(__file__)))
import utils

# Load Refresh Token
refresh_token = utils.load_config("refreshToken")

if not refresh_token:
    print("[ERROR] No refresh token found in secrets.json. Please login first.")
else:
    # Endpoint Configuration
    URL = f"{utils.BASE_URL}/auth/refresh-token"
    HEADERS = {
        "Authorization": f"Bearer {refresh_token}"
    }

    # Execute Request
    response = utils.send_and_print(
        url=URL,
        method="POST",
        headers=HEADERS,
        output_file=f"{os.path.splitext(os.path.basename(__file__))[0]}.json",
    )

    # Save new Access Token if successful
    if response.status_code == 200:
        data = response.json()
        if data and "accessToken" in data:
            print("\n[INFO] Saving new access token to secrets.json...")
            utils.save_config("accessToken", data.get("accessToken"))
            # Note: Refresh token might also be rotated depending on backend logic
            if "refreshToken" in data:
                utils.save_config("refreshToken", data.get("refreshToken"))