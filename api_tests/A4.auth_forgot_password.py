import sys
import os
sys.path.append(os.path.abspath(os.path.dirname(__file__)))
import utils

# Endpoint Configuration
URL = f"{utils.BASE_URL}/auth/forgot-password"
PAYLOAD = {
    "email": "john.doe@example.com"
}

# Execute Request
utils.send_and_print(
    url=URL,
    method="POST",
    body=PAYLOAD,
    output_file=f"{os.path.splitext(os.path.basename(__file__))[0]}.json",
)