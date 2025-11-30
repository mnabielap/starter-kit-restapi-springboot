import sys
import os
sys.path.append(os.path.abspath(os.path.dirname(__file__)))
import utils

# Endpoint Configuration
URL = f"{utils.BASE_URL}/auth/login"
PAYLOAD = {
    "email": "john.doe@example.com",
    "password": "password123"
}

# Execute Request
response = utils.send_and_print(
    url=URL,
    method="POST",
    body=PAYLOAD,
    output_file=f"{os.path.splitext(os.path.basename(__file__))[0]}.json",
)

# Save Tokens automatically if successful
if response.status_code == 200:
    data = response.json()
    if data:
        print("\n[INFO] Saving new tokens to secrets.json...")
        utils.save_config("accessToken", data.get("accessToken"))
        utils.save_config("refreshToken", data.get("refreshToken"))