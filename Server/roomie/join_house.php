<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("error" => FALSE);

if (isset($_POST['email']) && isset($_POST['api_key']) && isset($_POST['house_id']) && isset($_POST['house_password'])) {

    // receiving the post params
    $email = $_POST['email'];
    $api_key = $_POST['api_key'];
    $house_id = $_POST['house_id'];
    $house_password = $_POST['house_password'];

    if ($db->isValidApiKey($api_key, $email)) {

        $house = $db->getHouseByIdAndPassword($house_id, $house_password);

        if ($house) {

            $house_member = $db->registerUserInHouse($house["houseID"], $email, 0);

            $response["error"] = FALSE;
            $response["house"]["house_id"] = $house["houseID"];
            $response["house"]["house_name"] = $house["name"];
            $response["house"]["last_updated"] = $house["last_updated"];

            // getting the members from that house
            $house_members = $db->getMembersHouse($house_id);
            while ($row = $house_members->fetch_assoc()) {
                $response["members"]["member_" . $count]["email"] = $row["email"];
                $response["members"]["member_" . $count]["isAdmin"] = $row["isAdmin"];
                $count++;
            }
            echo json_encode($response);
        } else {
            $response["error"] = TRUE;
            $response["error_msg"] = "Is that a valid houseID or password ?";
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
    $response["error_msg"] = "Required parameters email, api_key, house_id or house_password is missing!";
    echo json_encode($response);
}
?>
