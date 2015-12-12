<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("error" => FALSE);

if (isset($_POST['email']) && isset($_POST['api_key'])) {

    // receiving the post params
    $email = $_POST['email'];
    $api_key = $_POST['api_key'];

    if ($db->isValidApiKey($api_key, $email)) {

        $houseID = $db->getHouseID($email);

        if ($houseID != NULL) {
            $db->deleteAllNotesFromUser($email, $houseID);
            $db->deleteHouseMember($houseID, $email);
            $response["error"] = FALSE;
            echo json_encode($response);
        } else {
            $response["error"] = TRUE;
            $response["error_msg"] = "User has no house!";
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
    $response["error_msg"] = "Required parameters email or api_key is missing!";
    echo json_encode($response);
}
?>
