<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("error" => FALSE);

if (isset($_POST['email']) && isset($_POST['api_key']) && isset($_POST['password'])) {

    // receiving the post params
    $email = $_POST['email'];
    $api_key = $_POST['api_key'];
    $password = $_POST['password'];

    if ($db->isValidApiKey($api_key, $email)) {
        $houseID = $db->getHouseID($email);
        if ($houseID != NULL) {
            $response["valid_house"] = TRUE;
            if ($db->isUserAdmin($houseID, $email)) {
                $house = $db->updateHousePassword($houseID, $password);
                echo json_encode($response);
            } else {
                $response["error"] = TRUE;
                $response["error_msg"] = "You are not an admin!";
                echo json_encode($response);
            }
            echo json_encode($response);
        } else {
            $response["valid_house"] = FALSE;
            echo json_encode($response);
        }
    } else {
        $response["error"] = TRUE;
        $response["error_msg"] = "Invalid API Key!";
        echo json_encode($response);
    }
} else {
    // required post params is missing
    $response["error"] = TRUE;
    $response["error_msg"] = "Required parameters email, api_key or password is missing!";
    echo json_encode($response);
}
?>
